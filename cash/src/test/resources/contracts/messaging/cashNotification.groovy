import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Notification of Cash Withdrawal Event"
    label "cash_notification_event"
    input {
        triggeredBy("triggerWithdrawal()")
    }
    outputMessage {
        sentTo "bank-app-notification"
        body([
                sourceService: "CASH",
                message      : "Снято 50,00 руб"
        ])
    }
}