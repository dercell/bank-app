import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Notification of Transfer Event"
    label "transfer_notification_event"
    input {
        triggeredBy("triggerTransfer()")
    }
    outputMessage {
        sentTo "bank-app-notification"
        body([
                sourceService: "TRANSFER",
                message      : "Перевод выполнен: 500,00 со счёта lukeAcc на счёт hanAcc"
        ])
    }
}

