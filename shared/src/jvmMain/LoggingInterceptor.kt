import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

internal class LoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val t1 = System.nanoTime()
        println(
            java.lang.String.format(
                "Sending request %s on %s%n%s%s",
                request.url, chain.connection(), request.headers, request.body
            )
        )
        val response: Response = chain.proceed(request)
        val t2 = System.nanoTime()
        println(
            String.format(
                "Received response for %s in %.1fms%n%s%s",
                response.request.url, (t2 - t1) / 1e6, response.headers, response.body
            )
        )

        return response
    }
}

