package fr.mipn.parc.actors

import akka.actor.{Actor, Props}
import io.swagger.server.model.{PlaceType, PricingPlan}



object FeeCalculator {
  var plans: Map[String, PricingPlan] = Map(
    "Starter" -> PricingPlan(
      "Starter",
      2.50d,
      1,
      List(PlaceType("voitures", "desc de type voitures"),
        PlaceType("velo", "desc de type velo")),
      3.0d
    ),
    "Premium" -> PricingPlan(
      "Premium",
      3.5d,
      5,
      List(PlaceType("voitures", "desc de type voitures")),
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
