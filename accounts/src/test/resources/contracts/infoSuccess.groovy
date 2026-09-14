package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Получение данных Люка Скайуокера"

    request {
        method 'GET'
        url '/accounts/info/luke'
    }

    response {
        status 200
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''
        {
            "userProfileDto":{
                "login":"luke",
                "username":"Luke Skywalker",
                "birthDate":"1990-01-15"
            },
            "curAccounts":[
                {"accountNumber":"qwe","balance":200}
            ],
            "accounts":[
                {
                    "login":"han",
                    "username":"Han Solo",
                    "accounts":[
                        {"accountNumber":"asd","balance":100}
                    ]
                }
            ]
        }
        '''
    }
}