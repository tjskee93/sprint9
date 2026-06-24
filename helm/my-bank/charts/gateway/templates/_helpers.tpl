{{- define "gateway.labels" -}}
app.kubernetes.io/name: gateway
app.kubernetes.io/component: microservice
app.kubernetes.io/part-of: bank
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "gateway.selectorLabels" -}}
app.kubernetes.io/name: gateway
app.kubernetes.io/component: microservice
{{- end -}}

{{- define "gateway.image" -}}
{{- $tag := .Values.image.tag -}}
{{- printf "%s:%s" .Values.image.repository $tag -}}
{{- end -}}