package titansocket

interface Mapper<T> {
    fun mapper(array: Array<out Any>): T
}
