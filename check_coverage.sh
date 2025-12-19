#!/bin/bash
for service in billing-service inventory-service kitchen-service menu-service order-service; do
  xmlfile="$service/build/reports/jacoco/test/jacocoTestReport.xml"
  if [ -f "$xmlfile" ]; then
    missed=$(grep '<counter type="LINE"' "$xmlfile" | tail -1 | grep -oP 'missed="\K[0-9]+')
    covered=$(grep '<counter type="LINE"' "$xmlfile" | tail -1 | grep -oP 'covered="\K[0-9]+')
    if [ -n "$covered" ] && [ -n "$missed" ]; then
      total=$((covered + missed))
      if [ $total -gt 0 ]; then
        percent=$((covered * 100 / total))
        echo "$service: $percent% ($covered/$total lines)"
      fi
    fi
  fi
done
