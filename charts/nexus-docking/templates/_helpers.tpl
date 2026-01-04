{{/*
Docking service specific helpers that extend nexus-common
*/}}

{{- define "nexus-docking.image" -}}
{{- include "nexus-common.image" . -}}
{{- end }}

{{- define "nexus-docking.labels" -}}
{{- include "nexus-common.labels" . }}
{{- end }}

{{- define "nexus-docking.selectorLabels" -}}
{{- include "nexus-common.selectorLabels" . }}
{{- end }}

{{- define "nexus-docking.imagePullSecrets" -}}
{{- include "nexus-common.imagePullSecrets" . }}
{{- end }}

{{/*
Generate power service URL
*/}}
{{- define "nexus-docking.powerUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.power "namespace" .Values.global.namespaces.power "port" .Values.global.ports.services) -}}
{{- end }}

{{/*
Generate crew service URL
*/}}
{{- define "nexus-docking.crewUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.crew "namespace" .Values.global.namespaces.crew "port" .Values.global.ports.services) -}}
{{- end }}

{{/*
Generate inventory service URL
*/}}
{{- define "nexus-docking.inventoryUrl" -}}
{{- include "nexus-common.serviceUrl" (dict "service" .Values.global.services.inventory "namespace" .Values.global.namespaces.inventory "port" .Values.global.ports.services) -}}
{{- end }}
