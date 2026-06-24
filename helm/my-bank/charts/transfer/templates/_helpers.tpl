{{- define "transfer.labels" -}}
app.kubernetes.io/name: transfer
app.kubernetes.io/component: microservice
app.kubernetes.io/part-of: bank
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "transfer.selectorLabels" -}}
app.kubernetes.io/name: transfer
app.kubernetes.io/component: microservice
{{- end -}}

{{- define "transfer.image" -}}
{{- $tag := .Values.image.tag | default .Values.global.image.tag -}}
{{- printf "%s:%s" .Values.image.repository $tag -}}
{{- end -}}