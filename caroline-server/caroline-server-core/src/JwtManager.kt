package cloud.caroline

import cloud.caroline.core.models.Permission
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

public object JwtManager {

    private lateinit var issuer: String
    private lateinit var realm: String
    private lateinit var secret: String

    private val algorithm by lazy { Algorithm.HMAC512(secret) }

    // TODO: Make configurable
    private val tokenLifeMs = TimeUnit.DAYS.toMillis(7)

    public fun configure(issuer: String, realm: String, secret: String) {
        check(!this::issuer.isInitialized) { "JwtManager already configured." }
        this.issuer = issuer
        this.realm = realm
        this.secret = secret
    }

    public fun verifier(): JWTVerifier {
        check(this::issuer.isInitialized) { "JwtManager must be configured." }
        return JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
    }

    public fun createToken(
        apiKey: String,
        permissions: Set<Permission>,
        expiresInMs: Long = (7 * 24 * 60 * 60)
    ): String {
        check(this::issuer.isInitialized) { "JwtManager must be configured." }
        val permissionArray = permissions.map(Permission::name).toTypedArray()
        return JWT.create()
            .withIssuer(issuer)
            .withArrayClaim("permissions", permissionArray)
            .withAudience(apiKey)
            .withIssuedAt(Date())
            .apply {
                if (expiresInMs > 0) {
                    withExpiresAt(Date.from(Instant.now().plusMillis(tokenLifeMs)))
                }
            }
            .sign(algorithm)
    }
}
