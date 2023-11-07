package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.HomeResponse
import com.example.model.LoginRequest
import com.example.model.LoginResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

val jwtAlgorithm: Algorithm = Algorithm.HMAC256("abcdef12abcdef12abcdef12abcdef12")
const val jwtIssuer = "ktor-api"
const val jwtExpirationPeriod = 3_600_000
val sessionUUID by lazy {
    UUID.randomUUID().toString()
}

val userDb = UserDb()

fun main() {
    embeddedServer(Netty, port = 8086, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        authentication {
            jwt {
                verifier(
                    JWT.require(jwtAlgorithm)
                        .withIssuer(jwtIssuer)
                        .build()
                )
                validate { credential ->
                    if (userDb.hasUser(credential.payload.subject)
//                        && credential.payload.getClaim("sessionUUID").asString() == sessionUUID
                    ) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }

        routing {
            post("/login") {
                val loginRequest = call.receive<LoginRequest>()
                val user = userDb.authenticate(loginRequest.username, loginRequest.password)
                if (user != null) {
                    call.respond(LoginResponse(user.username, generateToken(user.username)))
                } else {
                    call.respond(HttpStatusCode.Forbidden)
                }
            }

            authenticate {
                get("/home") {
                    val username = call.authentication.principal<JWTPrincipal>()!!.payload.subject
                    call.respond(HomeResponse(username, System.currentTimeMillis()))
                }
            }
        }
    }.start(wait = true)
}

fun generateToken(username: String): String {
    return JWT.create()
        .withIssuer(jwtIssuer)
        .withSubject(username)
        .withClaim("sessionUUID", sessionUUID)
        .withIssuedAt(Date())
        .withExpiresAt(Date(System.currentTimeMillis() + jwtExpirationPeriod))
        .sign(jwtAlgorithm)
}
