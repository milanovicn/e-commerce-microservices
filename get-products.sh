URL="http://localhost:8081/api/products"
REQ=500
CONC=1

for ((i=1; i<=REQ; i++)); do
  curl -sS -o /dev/null "$URL" &
  (( i % CONC == 0 )) && wait
done
wait