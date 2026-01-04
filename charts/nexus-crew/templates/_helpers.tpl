{{/*
Crew service specific helpers that extend nexus-common
*/}}

{{- define "nexus-crew.image" -}}
{{- include "nexus-common.image" . -}}
{{- end }}

{{- define "nexus-crew.labels" -}}
{{- include "nexus-common.labels" . }}
{{- end }}

{{- define "nexus-crew.selectorLabels" -}}
{{- include "nexus-common.selectorLabels" . }}
{{- end }}

{{- define "nexus-crew.imagePullSecrets" -}}
{{- include "nexus-common.imagePullSecrets" . }}
{{- end }}

{{/*
Generate life-support service URL
*/}}
{{- define "nexus-crew.lifeSupportUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.lifeSupport "namespace" .Values.global.namespaces.lifeSupport "port" .Values.global.ports.services) -}}
{{- end }}
