{{/*
Infra-specific helpers that extend nexus-common
*/}}

{{/*
Generate image path - wrapper around nexus-common.image
*/}}
{{- define "nexus-infra.image" -}}
{{- include "nexus-common.image" . -}}
{{- end }}

{{/*
Generate labels - wrapper around nexus-common.labels
*/}}
{{- define "nexus-infra.labels" -}}
{{- include "nexus-common.labels" . }}
{{- end }}

{{/*
Generate selector labels - wrapper around nexus-common.selectorLabels
*/}}
{{- define "nexus-infra.selectorLabels" -}}
{{- include "nexus-common.selectorLabels" . }}
{{- end }}

{{/*
Generate image pull secrets - wrapper around nexus-common.imagePullSecrets
*/}}
{{- define "nexus-infra.imagePullSecrets" -}}
{{- include "nexus-common.imagePullSecrets" . }}
{{- end }}
