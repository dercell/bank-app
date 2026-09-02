package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Негативный сценарий с отрицательной суммой пополнения"

    request {
        method 'PUT'
        url '/cash/han?action=PUT&sum=-1000'
    }

    response {
        status 500
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''
        {
            "message":"chargeSum.sum: must be greater than or equal to 0"
        }
        '''
    }
}