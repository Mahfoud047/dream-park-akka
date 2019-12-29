package fr.mipn.parc

import io.swagger.server.model.Place

object ResponseTypes {
  type EitherArrayPlace = Either[Error, List[Place]]
}
