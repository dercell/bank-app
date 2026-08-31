package contracts.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Должно начислиться 5к кредитов"

    request {
        method 'PUT'
        url '/accounts/charge/luke'
        headers {
            header 'Content-Type': 'application/x-www-form-urlencoded'
        }
        body 'action=deposit&sum=5000'
    }

    response {
        status 204
    }
}