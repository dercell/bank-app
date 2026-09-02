package contracts
import org.springframework.cloud.contract.spec.Contract



Contract.make {
    description "Перевод 500 кредитов от Люка Хану"

    request {
        method 'PUT'
        url '/accounts/transfer?from=luke&to=han&sum=500'
    }

    response {
        status 200
        headers {
            header 'Content-Type': 'application/json'
        }
        body '''
        {
            "message": "Перевод выполнен: 500 со счёта luke на счёт han"
        }
        '''
    }
}