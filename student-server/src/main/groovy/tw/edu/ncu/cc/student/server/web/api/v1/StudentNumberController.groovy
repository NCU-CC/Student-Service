package tw.edu.ncu.cc.student.server.web.api.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.convert.ConversionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tw.edu.ncu.cc.cardservice.entity.Faculty
import tw.edu.ncu.cc.cardservice.entity.INCUPerson
import tw.edu.ncu.cc.cardservice.entity.Student
import tw.edu.ncu.cc.cardservice.enums.PersonType
import tw.edu.ncu.cc.cardservice.service.INCUCardService
import tw.edu.ncu.cc.oauth.resource.exception.InvalidRequestException
import tw.edu.ncu.cc.student.data.person.FacultyObject
import tw.edu.ncu.cc.student.data.person.StudentObject
import tw.edu.ncu.cc.student.server.service.AccessProtectService

@RestController
@RequestMapping( value = "v1/cards" )
public class StudentNumberController {

    @Autowired
    def INCUCardService cardService

    @Autowired
    def ConversionService conversionService

    @Autowired
    def AccessProtectService accessProtectService

    @RequestMapping( value = "{cardNumber}", method = RequestMethod.GET )
    public Object findPersonByCardNumberAndIdNumber( @PathVariable( "cardNumber" ) String cardNumber,
                                                     @RequestParam( "id" ) String idLast4Digits,
                                                     Authentication authentication ) {

        if( ! accessProtectService.isValidKey( authentication.name ) ) {
            throw new InvalidRequestException( HttpStatus.FORBIDDEN, "forbidden" )
        }

        Optional< INCUPerson > data = cardService.findPersonByCardNo( cardNumber, idLast4Digits )
        if( data.present ) {
            INCUPerson person = data.get()
            if( person.type == PersonType.STUDENT ) {
                return conversionService.convert( ( Student ) person, StudentObject )
            } else {
                return conversionService.convert( ( Faculty ) person, FacultyObject )
            }
        } else {
            accessProtectService.registerError( authentication.name )
            return new ResponseEntity<>( "resource not found", HttpStatus.NOT_FOUND )
        }
    }

}