package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Негативный сценарий с отрицательной суммой пополнения"

    request {
        method 'PUT'
        url '/cash'
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''
        {
          "action" : "PUT",
          "accNumber" : "hanAcc",
          "sum" : -1000
        }
        '''
    }

    response {
        status 400
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''
        {
            "message":"sum: Сумма должна быть больше 0",
            "resultCode":"MethodArgumentNotValidException"
        }
        '''
    }
}