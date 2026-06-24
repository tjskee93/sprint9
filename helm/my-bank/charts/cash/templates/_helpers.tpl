{{- define "cash.labels" -}}
app.kubernetes.io/name: cash
app.kubernetes.io/component: microservice
app.kubernetes.io/part-of: bank
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "cash.selectorLabels" -}}
app.kubernetes.io/name: cash
app.kubernetes.io/component: microservice
{{- end -}}

{{- define "cash.image" -}}
{{- $tag := .Values.image.tag -}}
{{- printf "%s:%s" .Values.image.repository $tag -}}
{{- end -}}