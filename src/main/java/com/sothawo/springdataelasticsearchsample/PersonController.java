/*
 Copyright 2020 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.sothawo.springdataelasticsearchsample;

import com.devskiller.jfairy.Fairy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@RestController
@RequestMapping("/")
public class PersonController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonController.class);

    private PersonRepository personRepository;

    Fairy fairy = Fairy.create(Locale.ENGLISH);

    public PersonController(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @GetMapping("/persons/create/{count}")
    public void create(@PathVariable("count") Long count) {

        personRepository.deleteAll();


        long maxId = count;
        long fromId = 1L;

        while (fromId < maxId) {
            long toId = Math.min(fromId + 1000, maxId);

            List<Person> persons = LongStream.range(fromId, toId + 1)
                .mapToObj(this::createPerson)
                .collect(Collectors.toList());

            personRepository.saveAll(persons);

            fromId += 1000L;
        }
    }

    private Person createPerson(long id) {
        Person person = new Person();
        com.devskiller.jfairy.producer.person.Person fairyPerson = fairy.person();
        person.setId(id);
        person.setFirstName(fairyPerson.getFirstName());
        person.setLastName(fairyPerson.getLastName());
        person.setBirthDate(fairyPerson.getDateOfBirth());
        return person;
    }

    @PostMapping("/person")
    public String savePerson(@RequestBody final Person person) {
        return personRepository.save(person).getId().toString();
    }

    @GetMapping("/persons")
    public Iterable<Person> allPersons() {
        return personRepository.findAll();
    }

    @GetMapping("/persons/{name}")
    public SearchHits<Person> byName(@PathVariable("name") final String name) {
        SearchHits<Person> searchHits = personRepository.findByLastNameOrFirstName(name, name);
        LOG.debug("{}", searchHits);
        searchHits.forEach(searchHit -> LOG.debug("{}", searchHit));
        return searchHits;
    }

    @GetMapping("/person/{id}")
    public Person byId(@PathVariable("id") final Long id) {
        return personRepository.findById(id).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/persons/count")
    public long count() {
        return personRepository.count();
    }
}
