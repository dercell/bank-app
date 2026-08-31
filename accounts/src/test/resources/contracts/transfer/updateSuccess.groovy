package contracts.transfer


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Обновляет профиль Хана Соло"

    request {
        method 'PUT'
        url '/accounts/info/han?username=Han+Solo&birthdate=1970-07-15'
    }

    response {
        status 200
    }
}