{{- define "elasticsearch.labels" -}}
app.kubernetes.io/name: elasticsearch
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "elasticsearch.selectorLabels" -}}
app.kubernetes.io/name: elasticsearch
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
