package fr.mipn.parc.actors

import akka.actor.{Actor, Props}
import io.swagger.server.model.{PlaceType, PricingPlan}



object FeeCalculator {

  val placeTypes = List(PlaceType(1, "voitures", "desc de type voitures"),
    PlaceType(2, "velo", "desc de type velo"))

  var plans: Map[String, PricingPlan] = Map(
    "Starter" -> PricingPlan(
      "Starter",
      2.50d,
      1,
      List(1,2),
      3.0d
    ),
    "Premium" -> PricingPlan(
      "Premium",
      3.5d,
      5,
      List(1),
      1.0d
    )
  ).withDefaultValue(null)

  case object GetPricingPlans
  case class CheckPricingExists(pricingPlanName: String)

  def apply(): Props = Props(new FeeCalculator())
}

case class FeeCalculator() extends Actor {
  import FeeCalculator._

  override def receive: Receive = {

    case GetPricingPlans =>
      val plansList = plans.values.toList
      sender ! Right(plansList)

    case check: CheckPricingExists =>
      val exists = plans(check.pricingPlanName) != null
      sender ! Right(exists)

    case _@msg => sender ! s"I recieved the msg $msg"
  }
}
