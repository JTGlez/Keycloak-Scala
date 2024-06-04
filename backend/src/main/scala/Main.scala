
import cats.effect._
import cats.implicits._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client._
import org.http4s.client.dsl.io._
import org.http4s.client.blaze._
import io.circe.generic.auto._
import io.circe.syntax._
import org.typelevel.ci.CIString
object Main extends IOApp {
  case class ClientRepresentation(
     clientId: String,
     enabled: Boolean,
     clientAuthenticatorType: String,
     secret: String,
     redirectUris: List[String],
     protocol: String,
     publicClient: Boolean,
     directAccessGrantsEnabled: Boolean,
     serviceAccountsEnabled: Boolean
   )

  case class TokenResponse(access_token: String)
  case class AdminTokenRequest(username: String, password: String)

  case class CreateClientRequest(clientId: String, clientSecret: String)


  private val keycloakUrl = "http://localhost:8080"
  private val realm = "Secrets-Realm"
  private val adminUsername = "admin"
  private val adminPassword = "admin"

  private val client = BlazeClientBuilder[IO].resource

  private def getAdminToken(username: String, password: String): IO[String] = {
    val data = UrlForm(
      "grant_type" -> "password",
      "client_id" -> "admin-cli",
      "username" -> username,
      "password" -> password
    )

    client.use { httpClient =>
      val request = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"$keycloakUrl/realms/master/protocol/openid-connect/token")
      ).withEntity(data)

      httpClient.expect[TokenResponse](request).map(_.access_token)
    }
  }

  private def createClient(clientId: String, clientSecret: String, adminToken: String): IO[String] = {
    val clientRepresentation = ClientRepresentation(
      clientId = clientId,
      enabled = true,
      clientAuthenticatorType = "client-secret",
      secret = clientSecret,
      redirectUris = List("https://localhost:8080/*"),
      protocol = "openid-connect",
      publicClient = false,
      directAccessGrantsEnabled = true,
      serviceAccountsEnabled = true
    )

    client.use { httpClient =>
      val request = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"$keycloakUrl/auth/admin/realms/$realm/clients")
      ).withHeaders(
        Header.Raw(CIString("Authorization"), s"Bearer $adminToken"),
        Header.Raw(CIString("Content-Type"), "application/json")
      ).withEntity(clientRepresentation.asJson)

      httpClient.expect[String](request)
    }
  }

  private def getToken(clientId: String, clientSecret: String): IO[TokenResponse] = {
    val data = UrlForm(
      "grant_type" -> "client_credentials",
      "client_id" -> clientId,
      "client_secret" -> clientSecret
    )

    client.use { httpClient =>
      val request = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"$keycloakUrl/auth/realms/$realm/protocol/openid-connect/token")
      ).withEntity(data)

      httpClient.expect[TokenResponse](request)
    }
  }

  private val createClientService = HttpRoutes.of[IO] {
    case req@POST -> Root / "create-client" =>
      for {
        createClientRequest <- req.as[CreateClientRequest]
        adminToken <- getAdminToken(adminUsername, adminPassword)
        createResponse <- createClient(createClientRequest.clientId, createClientRequest.clientSecret, adminToken)
        response <- Ok(s"Client ${createClientRequest.clientId} created with secret ${createClientRequest.clientSecret}")
      } yield response
  }

  private val getTokenService = HttpRoutes.of[IO] {
    case req @ POST -> Root / "get-token" =>
      for {
        clientId <- req.as[String]
        clientSecret = "my-client-secret" // This should match the secret used in create-client
        tokenResponse <- getToken(clientId, clientSecret)
        response <- Ok(tokenResponse.access_token)
      } yield response
  }

  private val adminTokenService = HttpRoutes.of[IO] {
    case req @ POST -> Root / "admin-token" =>
      for {
        adminTokenRequest <- req.as[AdminTokenRequest]
        token <- getAdminToken(adminTokenRequest.username, adminTokenRequest.password)
        response <- Ok(token)
      } yield response
  }

  private val helloWorldService = HttpRoutes.of[IO] {
    case GET -> Root / "hello" => Ok("Hello, Cats!")
  }

  private val httpApp = Router(
    "/" -> (helloWorldService <+> createClientService <+> getTokenService <+> adminTokenService)
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO]
      .bindHttp(8081, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}

