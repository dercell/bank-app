package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Должно начислиться 5к кредитов"

    request {
        method 'PUT'
        url '/cash'
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''
        {
          "action" : "PUT",
          "accNumber" : "lukeAcc",
          "sum" : 5000
        }
        '''
    }

    response {
        status 204
    }
}