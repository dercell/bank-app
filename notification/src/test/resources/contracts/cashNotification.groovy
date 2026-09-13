package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Notification of Cash Event"
    label "log_entity_event"
    input {
        triggeredBy("triggerLogEntity()")
    }
    outputMessage {
        sentTo "bank-app-notification"
        body([
                sourceService: "CASH",
                message      : "Снято 50,00 руб"
        ])
    }
}