; тест комментария
(define two 2)

(define ego (person 'Евгений' 'Грязнов'))

(define subtract (lambda (a b)
                   (+ a (* -1 b))
                 )
)

(define void? (lambda (obj)
                (of-type? obj 'void')
              )
)

; Инкремент
(define inc (lambda (n)
               (+ n 1)
             )
)

; Декремент
(define dec (lambda (n)
              (subtract n 1)
            )
)



(define first-n (lambda (n)
                  (if (= n 0)
                    (list 0)
                    (append (first-n (dec n)) n)
                  )
                )
)
