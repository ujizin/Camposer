package com.ujizin.sample.data.mapper

import com.ujizin.sample.domain.User
import com.ujizin.sample.data.local.User as LocalUser

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