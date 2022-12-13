package network.zenon.android

import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.logging.Logger
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

const val ZNN_SDK_VERSION = "0.0.4"
const val ZNN_ROOT_DIRECTORY = "znn"

class ZnnPaths(
    var main: Path,
    var wallet: Path,
    var cache: Path
) {
    companion object {
        var DEFAULT = default
        private val default: ZnnPaths
            get() {
                var osName = System.getProperty("os.name")
                osName = osName?.lowercase(Locale.getDefault()) ?: ""
                val main: Path =
                    if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
                        Paths.get(System.getenv("HOME"), ".$ZNN_ROOT_DIRECTORY")
                    } else if (osName.contains("mac")) {
                        Paths.get(System.getenv("HOME"), "Library", ZNN_ROOT_DIRECTORY)
                    } else if (osName.contains("win")) {
                        Paths.get(System.getenv("APPDATA"), ZNN_ROOT_DIRECTORY)
                    } else {
                        Paths.get(System.getenv("HOME"), ZNN_ROOT_DIRECTORY)
                    }
                return ZnnPaths(
                    main,
                    main.resolve("wallet"),
                    main.resolve("syrius")
                )
            }
    }
}

val ZNN_DEFAULT_PATHS: ZnnPaths = ZnnPaths.DEFAULT
val ZNN_DEFAULT_DIRECTORY: Path = ZNN_DEFAULT_PATHS.main
val ZNN_DEFAULT_WALLET_DIRECTORY: Path = ZNN_DEFAULT_PATHS.wallet
val ZNN_DEFAULT_CACHE_DIRECTORY: Path = ZNN_DEFAULT_PATHS.cache

fun ensureDirectoriesExist() {
    if(!ZNN_DEFAULT_WALLET_DIRECTORY.exists()) {
        ZNN_DEFAULT_WALLET_DIRECTORY.createDirectory()
    }
    if(!ZNN_DEFAULT_CACHE_DIRECTORY.exists()) {
        ZNN_DEFAULT_CACHE_DIRECTORY.createDirectory()
    }
}

const val NET_ID = 1 // Alphanet

val LOGGER: Logger = Logger.getLogger("ZNN-SDK")

open class ZnnSdkException(message: String? = null)
    : RuntimeException(if(null != message) "$DEFAULT_MESSAGE: $message" else DEFAULT_MESSAGE) {
    companion object {
        private const val DEFAULT_MESSAGE = "Zenon SDK Exception"
    }
}

