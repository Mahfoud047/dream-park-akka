package io.swagger.server.input_model

import io.swagger.server.model.Reservation

case class PostReservation(
                            placeId: Int,
                            startTime: String,
                            pricingPlanName: String,
                            endTime: Option[String],
                            licensePlate: Option[String],
                            phoneNumber: Option[String]
                          ) {
  def toReservation(id: Int): Reservation = {
    Reservation(
      id,
      this.placeId,
      this.startTime,
      this.endTime,
      this.pricingPlanName,
      this.licensePlate,
      this.phoneNumber
    )
  }
}

