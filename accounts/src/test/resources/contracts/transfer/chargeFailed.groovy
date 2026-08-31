package contracts.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Негативный сценарий с отрицательной суммой пополнения"

    request {
        method 'PUT'
        url '/accounts/charge/han?action=deposit&sum=-1000'
    }

    response {
        status 500
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''
        {
            "message":"chargeBalance.sum: должно быть не меньше 0"
        }
        '''
    }
}