import json
import re



file_json = open('lisp-doc.json', 'r')
latex_file = open('doc.tex', 'w')
doc = json.load(file_json)
# Start parsing json
for key in doc.keys():
    latex_file.write('\subsection{%s}\n' % key)
    latex_file.write('\\begin{itemize}\n')
    fun_entry = doc[key]
    # Write Name, Signature and Description items
    if 'Name' in fun_entry:
        latex_file.write('    \item Name: %s \n' % fun_entry['Name'])
    if 'Signature' in fun_entry:
        latex_file.write('    \item Signature: \\texttt{%s}\n' % fun_entry['Signature'])
    if 'Description' in fun_entry:
        latex_file.write('    \item Description: %s\n' % fun_entry['Description'])
    # Write Arguments item
    if 'Arguments' in fun_entry:
        latex_file.write('    \item Arguments: \n')
        latex_file.write('        \\begin{itemize}\n')
        # Write all arguments in fun_entry['Arguments'] dict
        args = fun_entry['Arguments']
        for key_arg in args.keys():
            latex_file.write('            \item \\textbf{%s} : %s\n' % (key_arg, args[key_arg]))
        latex_file.write('        \end{itemize}\n')
    # Examples writing
    if 'Examples' in fun_entry:
        latex_file.write('    \item Examples :\n')
        latex_file.write('        \\begin{itemize}\n')
        examples = fun_entry['Examples']
        # Write all elements of a list `examples` as items
        for item in examples:
            latex_file.write('            \item \\texttt{%s}\n' % item)
        latex_file.write('        \end{itemize}\n')
    # Type writing
    if 'Type' in fun_entry:
        latex_file.write('    \item Type : \[%s\]\n' % fun_entry['Type'])
    # Notes Writing
    if 'Notes' in fun_entry:
        notes = fun_entry['Notes']
        if isinstance(notes, list):
            notes = ' '.join(notes)
        latex_file.write('    \item Notes : %s\n' % notes)
    #
    latex_file.write('\end{itemize}\n')
    latex_file.write('\n')
    #
#
latex_file.close()
