package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Notification of Accounts Event"
    label "log_entity_event"
    input {
        triggeredBy("accountsNotification()")
    }
    outputMessage {
        sentTo "bank-app-notification"
        body([
                sourceService: "ACCOUNTS",
                message      : "Профиль luke обновлен"
        ])
    }
}