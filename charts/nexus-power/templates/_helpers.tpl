{{/*
Power service specific helpers that extend nexus-common
*/}}

{{/*
Generate image path - wrapper around nexus-common.image
*/}}
{{- define "nexus-power.image" -}}
{{- include "nexus-common.image" . -}}
{{- end }}

{{/*
Generate labels - wrapper around nexus-common.labels
*/}}
{{- define "nexus-power.labels" -}}
{{- include "nexus-common.labels" . }}
{{- end }}

{{/*
Generate selector labels - wrapper around nexus-common.selectorLabels
*/}}
{{- define "nexus-power.selectorLabels" -}}
{{- include "nexus-common.selectorLabels" . }}
{{- end }}

{{/*
Generate image pull secrets - wrapper around nexus-common.imagePullSecrets
*/}}
{{- define "nexus-power.imagePullSecrets" -}}
{{- include "nexus-common.imagePullSecrets" . }}
{{- end }}
