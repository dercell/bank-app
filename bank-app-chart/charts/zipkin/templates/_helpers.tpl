{{- define "zipkin.fullname" -}}
{{- $name := .Chart.Name }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{- define "zipkin.labels" -}}
app.kubernetes.io/name: zipkin
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "zipkin.selectorLabels" -}}
app.kubernetes.io/name: zipkin
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
