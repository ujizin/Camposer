package br.com.devlucasyuji.sample.data.mapper

import br.com.devlucasyuji.sample.domain.User
import br.com.devlucasyuji.sample.data.local.User as LocalUser

class UserMapper {

    fun toDomain(user: LocalUser) = with(user) {
        User(
            usePinchToZoom = usePinchToZoom,
            useTapToFocus = useTapToFocus,
            useCamFront = useCamFront
        )
    }

    fun toLocal(user: User) = with(user) {
        LocalUser(
            usePinchToZoom = usePinchToZoom,
            useTapToFocus = useTapToFocus,
            useCamFront = useCamFront
        )
    }
}