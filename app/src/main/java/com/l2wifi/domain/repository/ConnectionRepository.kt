override suspend fun connect(account: Account): Flow<Result<Unit>> = flow {
    try {
        val response = nautaApi.login(...)
        if (response.isSuccessful && response.body()?.success == true) {
            // éxito
        } else {
            val errorMsg = response.body()?.message ?: "Credenciales incorrectas"
            emit(Result.failure(Exception(errorMsg)))
        }
    } catch (e: IOException) {
        emit(Result.failure(Exception("Error de red: ${e.message}")))
    } catch (e: Exception) {
        emit(Result.failure(e))
    }
}