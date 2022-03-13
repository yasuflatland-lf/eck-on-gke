package design.studio.content.search.service.elasticsearch.connection

/**
 * @author Yasuyuki Takeo
 */
class ElasticsearchConnectionBuilder(
    val _connectionId: String?,
    val _serverName: String?,
    val _port: Int?,
    val _username: String?,
    val _password: String?,
    var _caPath: String?,
    val _active: Boolean?
) {
    data class Builder(
        var _connectionId: String? = "",
        var _serverName: String? = "",
        var _port: Int? = Int.MIN_VALUE,
        var _username: String? = "",
        var _password: String? = "",
        var _caPath: String? = "",
        var _active: Boolean? = false
    ) {

        fun connectionId(_connectionId: String) = apply { this._connectionId = _connectionId }
        fun serverName(_serverName: String) = apply { this._serverName = _serverName }
        fun port(_port: Int) = apply { this._port = _port }
        fun username(_username: String) = apply { this._username = _username }
        fun password(_password: String) = apply { this._password = _password }
        fun caPath(_caPath: String) = apply { this._caPath = _caPath }
        fun active(_active: Boolean) = apply { this._active = _active }

        fun build() = ElasticsearchConnection(
            _connectionId,
            _serverName,
            _port,
            _username,
            _password,
            _caPath,
            _active
        )
    }
}