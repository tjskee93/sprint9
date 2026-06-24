{{- define "accounts.labels" -}}
app.kubernetes.io/name: accounts
app.kubernetes.io/component: microservice
app.kubernetes.io/part-of: bank
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "accounts.selectorLabels" -}}
app.kubernetes.io/name: accounts
app.kubernetes.io/component: microservice
{{- end -}}

{{- define "accounts.image" -}}
{{- $tag := .Values.image.tag -}}
{{- printf "%s:%s" .Values.image.repository $tag -}}
{{- end -}}