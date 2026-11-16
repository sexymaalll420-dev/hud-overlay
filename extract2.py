#!/usr/bin/env python3
import re
from pathlib import Path

src = Path('/root/hud_app/123_.txt')
text = src.read_text(encoding='utf-8', errors='replace')

pattern = re.compile(r"^\s*cat > ([^\n]+?) <<'XEOF'\s*$", re.MULTILINE)
count = 0
for m in pattern.finditer(text):
    rel = m.group(1).strip()
    start = m.end() + 1  # after newline
    # find XEOF line
    end = text.find('\n          XEOF', start)
    if end < 0:
        end = text.find('\nXEOF', start)
    if end < 0:
        continue
    content = text[start:end]
    out = Path('/root/hud_app') / rel
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(content, encoding='utf-8')
    count += 1
    print('wrote', rel)
print('done', count)
