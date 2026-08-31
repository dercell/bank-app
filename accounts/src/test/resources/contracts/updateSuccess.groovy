package contracts


import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Обновляет профиль Люка Скайуокера"

    request {
        method 'PUT'
        url '/accounts/info/luke?username=Luke%20Starkiller&birthdate=1970-01-15'
    }

    response {
        status 200
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''
        {
            "curAccount":
                {
                    "id":null,
                    "login":"luke",
                    "username":"Luke Starkiller",
                    "birthDate":"1970-01-15",
                    "balance":1000
                },
            "accounts":[
                {
                    "login":"han",
                    "username":"Han Solo"
                }]
            }
        '''
    }
}