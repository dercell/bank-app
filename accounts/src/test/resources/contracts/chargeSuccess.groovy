package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Должно начислиться 5к кредитов"

    request {
        method 'PUT'
        url '/accounts/charge/luke?action=PUT&sum=5000'
    }

    response {
        status 204
    }
}