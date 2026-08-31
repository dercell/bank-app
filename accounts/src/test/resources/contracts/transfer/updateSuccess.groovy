package contracts.transfer


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Обновляет профиль Хана Соло"

    request {
        method 'PUT'
        url '/accounts/info/han'
        headers {
            header 'Content-Type': 'application/x-www-form-urlencoded'
        }
        body 'username=Han+Solo&birthdate=1970-07-15'
    }

    response {
        status 200
    }
}