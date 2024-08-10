package data

@Suppress("Unused")
interface NoReflect {
  fun fieldGetters(): Map<String, () -> Any?>
}
