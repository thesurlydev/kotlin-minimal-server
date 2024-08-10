package repository

@Suppress("Unused")
interface Repository<S, T> {
  fun findAll(): List<T>
  fun findById(id: S): T?
  fun save(entity: T): T?
  fun delete(id: S): Boolean
}