export default function StatusBadge({ status }) {
  const className = `status status-${status.toLowerCase().replaceAll('_', '-')}`
  return <span className={className}>{status.replaceAll('_', ' ')}</span>
}
