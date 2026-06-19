{{- define "front-ui.labels" -}}
app.kubernetes.io/name: front-ui
app.kubernetes.io/component: microservice
app.kubernetes.io/part-of: bank
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "front-ui.selectorLabels" -}}
app.kubernetes.io/name: front-ui
app.kubernetes.io/component: microservice
{{- end -}}

{{- define "front-ui.image" -}}
{{- $tag := .Values.image.tag -}}
{{- printf "%s:%s" .Values.image.repository $tag -}}
{{- end -}}