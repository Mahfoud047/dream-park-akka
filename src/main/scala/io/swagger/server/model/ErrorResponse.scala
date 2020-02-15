package io.swagger.server.model

/**
 * @param message 
 */
case class ErrorResponse(
  message: String
){
  
}


//case class Error400(override val message: String = "bad input") extends Error(message)
