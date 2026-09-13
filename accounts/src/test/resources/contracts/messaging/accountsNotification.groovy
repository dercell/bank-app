import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Notification of Account Update Event"
    label "accounts_notification_event"
    input {
        triggeredBy("triggerAccountUpdate()")
    }
    outputMessage {
        sentTo "bank-app-notification"
        body([
                sourceService: "ACCOUNTS",
                message      : "Профиль luke обновлен"
        ])
    }
}