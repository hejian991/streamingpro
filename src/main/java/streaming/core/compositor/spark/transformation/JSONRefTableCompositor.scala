package streaming.core.compositor.spark.transformation

import java.util

import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}
import serviceframework.dispatcher.{Compositor, Processor, Strategy}
import streaming.core.compositor.spark.streaming.CompositorHelper
import streaming.core.strategy.ParamsValidator

import scala.collection.JavaConversions._


class JSONRefTableCompositor[T] extends Compositor[T] with CompositorHelper with ParamsValidator {

  private var _configParams: util.List[util.Map[Any, Any]] = _
  val logger = Logger.getLogger(classOf[JSONRefTableCompositor[T]].getName)

  override def initialize(typeFilters: util.List[String], configParams: util.List[util.Map[Any, Any]]): Unit = {
    this._configParams = configParams
  }


  def tableName = {
    config[String]("tableName", _configParams)
  }


  override def result(alg: util.List[Processor[T]], ref: util.List[Strategy[T]], middleResult: util.List[T], params: util.Map[Any, Any]): util.List[T] = {
    val _tableName = tableName.get

    val newDF = middleResult.get(0) match {
      case rdd: RDD[String] =>
        sqlContextHolder(params).read.json(rdd)

      case df: DataFrame => df
    }
    newDF.registerTempTable(_tableName)
    List(newDF.asInstanceOf[T])
  }

  override def valid(params: util.Map[Any, Any]): (Boolean, String) = {
    if (tableName.isDefined) (true, "")
    else
      (false, s"Job name = ${params("_client_")}, Compositor=JSONTableCompositor,Message = tableName required in JSONTableCompositor")
  }
}
