{{- define "notifications.labels" -}}
app.kubernetes.io/name: notifications
app.kubernetes.io/component: microservice
app.kubernetes.io/part-of: bank
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "notifications.selectorLabels" -}}
app.kubernetes.io/name: notifications
app.kubernetes.io/component: microservice
{{- end -}}

{{- define "notifications.image" -}}
{{- $tag := .Values.image.tag | default .Values.global.image.tag -}}
{{- printf "%s:%s" .Values.image.repository $tag -}}
{{- end -}}